package com.blanke.downloadprogress;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class RecyclerViewActivity extends AppCompatActivity {
    List<Data> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view);
        RecyclerView recyclerView = findViewById(R.id.recyclerView);

        list.add(new Data(Observable.interval(700, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())));
        list.add(new Data(Observable.interval(700, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())));

        list.add(new Data(Observable.interval(700, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())));
        list.add(new Data(Observable.interval(700, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())));

        list.add(new Data(Observable.interval(700, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())));
        list.add(new Data(Observable.interval(700, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())));

        list.add(new Data(Observable.interval(700, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())));
        list.add(new Data(Observable.interval(700, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())));

        list.add(new Data(Observable.interval(700, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())));
        list.add(new Data(Observable.interval(700, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())));

        list.add(new Data(Observable.interval(700, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())));
        list.add(new Data(Observable.interval(700, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())));

        MyAdapter adapter = new MyAdapter(list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        List<Data> list;

        MyAdapter(List<Data> list) {
            this.list = list;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_content, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final DownloadProgressButton downloadButton = holder.download;
            downloadButton.setOnDownLoadClickListener(null);
            downloadButton.setEnablePause(true);
            final Data data = list.get(position);
            downloadButton.setState(data.state);
            downloadButton.setProgress(data.progress);
            downloadButton.setOnDownLoadClickListener(new DownloadProgressButton.OnDownLoadClickListener() {
                @Override
                public void waiting() {

                }

                @Override
                public void downloading() {
                    Action1<Long> action = new Action1<Long>() {
                        @Override
                        public void call(Long aLong) {
                            if (data.state == DownloadProgressButton.FINISH) {
                                data.sub.unsubscribe();
                                data.state = DownloadProgressButton.FINISH;
                                return;
                            }
                            int p = new Random().nextInt(20);
                            downloadButton.setProgress(data.progress + p);
                            data.progress = downloadButton.getProgress();
                            data.state = downloadButton.getState();
                        }
                    };
                    data.sub = data.observable.subscribe(action);
                }

                @Override
                public void pause() {
                    data.sub.unsubscribe();
                    data.progress = downloadButton.getProgress();
                    data.state = downloadButton.getState();
                }

                @Override
                public void resume() {
                    downloading();
                }

                @Override
                public void installing() {

                }

                @Override
                public void finished() {
                    data.sub.unsubscribe();
                    data.progress = downloadButton.getProgress();
                    data.state = downloadButton.getState();
                }
            });
        }

        @Override
        public int getItemCount() {
            return list.size();
        }
    }

    private class MyViewHolder extends RecyclerView.ViewHolder {
        DownloadProgressButton download;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);
            download = itemView.findViewById(R.id.download);
        }
    }
}
