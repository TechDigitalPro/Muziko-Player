

package com.whitebird.music.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Process;

import com.whitebird.music.activities.splash.SplashActivity;
import java.io.PrintWriter;
import java.io.StringWriter;


public class ExceptionHandler implements
        Thread.UncaughtExceptionHandler {
    public static final String CRASH_REPORT = "crashReport";
    private final Context myContext;

    public ExceptionHandler(Context context) {

        myContext = context;
    }

    @Override
    public void uncaughtException(Thread thread, Throwable exception) {

        final StringWriter stackTrace = new StringWriter();
        exception.printStackTrace(new PrintWriter(stackTrace));

        System.err.println(stackTrace);// You can use LogCat too

        Intent intent = new Intent(myContext, SplashActivity.class);
        intent.putExtra(CRASH_REPORT, stackTrace.toString());
        myContext.startActivity(intent);

        Process.killProcess(Process.myPid());
        System.exit(10);
    }

}