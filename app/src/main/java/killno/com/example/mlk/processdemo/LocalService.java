package killno.com.example.mlk.processdemo;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

/**
 * 作者：mlk on 2018/2/12 10:15
 */
public class LocalService extends Service {

    private static final String TAG = LocalService.class.getSimpleName();
    private MyBinder binder;
    private MyConn conn;
    private Thread t1;
    private boolean isStop = true;
    private Intent intent;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        binder = new MyBinder();
        if(conn == null){
            conn = new MyConn();
        }
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        this.intent = new Intent(this, RemoteService.class);
        this.bindService(this.intent,conn, Context.BIND_IMPORTANT);
        t1 = new Thread(){
            @Override
            public void run() {
                super.run();
                while (isStop){
                    SystemClock.sleep(1000);
                    Log.d(TAG, "run: 本地服务还在运行");
                }
            }
        };
        t1.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isStop = false;
        unbindService(conn);
    }

    class MyBinder extends ProcessService.Stub{

        @Override
        public String getServiceName() throws RemoteException {
            return "LocalService";
        }
    }

    class MyConn implements ServiceConnection{

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            Log.e(TAG, "连接远程服务成功");
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Toast.makeText(LocalService.this, "远程服务被干掉", Toast.LENGTH_SHORT).show();
            //远程服务被杀死时，开启远程服务  并绑定
            LocalService.this.startService(new Intent(LocalService.this,RemoteService.class));
            LocalService.this.bindService(new Intent(LocalService.this,RemoteService.class),conn,Context.BIND_IMPORTANT);

        }
    }

}
