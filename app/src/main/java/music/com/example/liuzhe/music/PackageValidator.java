package music.com.example.liuzhe.music;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.os.Process;
import android.util.Base64;
import android.util.Log;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by liuzhe on 2016/6/7.
 */
public class PackageValidator {

    private static final String TAG = "PackageValidator";
    private final Map<String, ArrayList<CallerInfo>> mValidCertificates;

    public PackageValidator(Context context){
        mValidCertificates = readValidCertificates(context.getResources().getXml(
                R.xml.allowed_media_browser_callers));

    }

    //解析xml文件并保存一个HASH_MAP
    private Map<String, ArrayList<CallerInfo>> readValidCertificates(XmlResourceParser parser) {
        HashMap<String, ArrayList<CallerInfo>> certificates = new HashMap<>();

        try {
            int envenType = parser.next();
            while (envenType != XmlResourceParser.END_DOCUMENT){
                if(envenType == XmlResourceParser.START_TAG && parser.getName().equals("signing_certificate")){
                    String name = parser.getAttributeValue(null, "name");
                    String packageName = parser.getAttributeValue(null, "package");
                    Boolean isRelease = parser.getAttributeBooleanValue(null, "release", false);
                    String certificate = parser.nextText().replaceAll("\\s|\\n", "");

                    CallerInfo info = new CallerInfo(name, packageName, isRelease, certificate);

                    ArrayList<CallerInfo> infos = certificates.get(certificate);

                    //如果没有保存过，则新建一个空的
                    if(infos == null){
                        infos = new ArrayList<>();
                        certificates.put(certificate, infos);
                    }
                    //更新certificate对应的callerinfo
                    infos.add(info);
                }
                envenType = parser.next();
            }
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return certificates;
    }


    public boolean isCallerAllowed (Context context, String callingPackage, int callingUid){
        //允许当前包内的fragment及当前APP或开发环境调用
        if(Process.SYSTEM_UID == callingUid || Process.myUid() == callingUid ){
            return true;
        }

        //检查package是否存在
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        try {
            packageInfo = packageManager.getPackageInfo(callingPackage, PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
        //检查package的签名
        if(packageInfo.signatures.length != 1){
            return false;
        }

        String signature = Base64.encodeToString(packageInfo.signatures[0].toByteArray(), Base64.NO_WRAP);
        ArrayList<CallerInfo> validCaller = mValidCertificates.get(signature);

        //检查是否可查找到允许的签名
        if(validCaller == null){

            if(mValidCertificates.isEmpty()){
                Log.i(TAG, "The list of valid certificates is empty. Either your file \",\n" +
                        " \"res/xml/allowed_media_browser_callers.xml is empty or there was an error \",\n" +
                        " \"while reading it. Check previous log messages.");
            }

            return false;
        }

        //若存在签名，检查是否相应的package
        StringBuffer execPackage = new StringBuffer();
        for(CallerInfo caller : validCaller){
            //若存在返回true
            if(callingPackage.equals(caller.packageName)){
                return true;
            }
        }
        return false;
    }


    //定义Call musicservice的对象
    private final static class CallerInfo {
        final String name;
        final String packageName;
        final boolean release;
        final String signingCertificate;

        public CallerInfo(String name, String packageName, boolean release,
                          String signingCertificate) {
            this.name = name;
            this.packageName = packageName;
            this.release = release;
            this.signingCertificate = signingCertificate;
        }
    }
}
