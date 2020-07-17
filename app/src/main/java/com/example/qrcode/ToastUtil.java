package com.example.qrcode;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Please enter the class description...
 *
 * @author Alvin
 * @version 1.0 2018/6/25
 */
public class ToastUtil {

  /*  private final static Handler HANDLER = new ToastHandler();

    private static class ToastHandler extends Handler {
        private static final Object synObj = new Object();

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            synchronized (synObj) {
                toast.setText((String) msg.obj);
                toast.setDuration(msg.arg1);
                toast.setGravity(msg.arg2, 0, 0);
                toast.show();
            }
        }
    }*/

    private static Toast toast;
    private static Context context;

    @SuppressLint("ShowToast")
    public static void init(Context context) {
        ToastUtil.context = context;
        toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
    }

    public static void showMessageOnCenter(final String msg) {
        showMessage(msg, Toast.LENGTH_SHORT, Gravity.CENTER);
    }

    public static void showMessageOnCenter(final int resId) {
        showMessage(resId, Toast.LENGTH_SHORT, Gravity.CENTER);
    }

    public static void showMessage(final String msg) {
        showMessage(msg, Toast.LENGTH_SHORT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
    }

    public static void showMessage(final int resId) {
        showMessage(resId, Toast.LENGTH_SHORT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
    }

    public static void showMessage(final int resId, final int len, int gravity) {
        showMessage(context.getString(resId), len, gravity);
    }

    public static void showMessage(final String msg, final int len, int gravity) {
      /*  final Message message = HANDLER.obtainMessage();
        message.arg1 = len;
        message.arg2 = gravity;
        message.obj = msg;
        HANDLER.sendMessage(message);*/
        toast.setText(msg);
        toast.setDuration(len);
        toast.setGravity(gravity, 0, 0);
        toast.show();
    }

    @SuppressWarnings("unused")
    public static void cancel() {
        toast.cancel();
    }
}
