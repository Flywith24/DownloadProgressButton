package com.blanke.downloadprogress;

import rx.Observable;
import rx.Subscription;

/**
 * @author yyz (杨云召)
 * @date 2020-01-05
 * time   15:41
 * description
 */
class Data {
    Observable<Long> observable;
    float progress = -1;
    int state = 1;
    Subscription sub;


    Data(Observable<Long> observable) {
        this.observable = observable;
    }
}
