package com.blanke.downloadprogress;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class MainActivity extends AppCompatActivity {
    private DownloadProgressButton downloadButton;
    private Subscription sub;
    private Button resetButton;
    private Observable<Long> obser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        downloadButton = (DownloadProgressButton) findViewById(R.id.download);
        resetButton = (Button) findViewById(R.id.reset);
        downloadButton.setEnablePause(true);

        obser = Observable.interval(700, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread());

        downloadButton.setOnDownLoadClickListener(new DownloadProgressButton.OnDownLoadClickListener() {
            @Override
            public void waiting() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        downloadButton.downloading();
                    }
                }, 3000);
            }

            @Override
            public void downloading() {
                sub = obser.subscribe(new Action1<Long>() {
                    @Override
                    public void call(Long aLong) {
                        if (downloadButton.getState() == DownloadProgressButton.FINISH) {
                            sub.unsubscribe();
                            return;
                        }
                        int p = new Random().nextInt(20);
                        downloadButton.setProgress(downloadButton.getProgress() + p);
                    }
                });
            }

            @Override
            public void clickPause() {
                sub.unsubscribe();
            }

            @Override
            public void clickResume() {
                downloading();
            }

            @Override
            public void installing() {
                sub.unsubscribe();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        downloadButton.finish();
                    }
                }, 2000);
            }

            @Override
            public void clickFinish() {
            }
        });
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sub != null) {
                    sub.unsubscribe();
                }
                downloadButton.reset();
            }
        });
    }
}
