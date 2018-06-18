package org.fossasia.openevent.app.core.orders.list;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import org.fossasia.openevent.app.data.order.Order;
import org.fossasia.openevent.app.data.order.OrderRepository;
import org.fossasia.openevent.app.utils.ErrorUtils;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;

import static org.fossasia.openevent.app.common.rx.ViewTransformers.dispose;

public class OrdersViewModel extends ViewModel {

    private final OrderRepository orderRepository;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    private MutableLiveData<List<Order>> ordersLiveData = new MutableLiveData<>();
    private MutableLiveData<Boolean> progress = new MutableLiveData<>();
    private MutableLiveData<String> error = new MutableLiveData<>();

    @Inject
    public OrdersViewModel(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
        progress.setValue(false);
    }

    public LiveData<List<Order>> getOrders(long id, boolean reload) {
        if (ordersLiveData.getValue() != null && !reload)
            return ordersLiveData;

        compositeDisposable.add(orderRepository.getOrders(id, reload)
            .compose(dispose(compositeDisposable))
            .doOnSubscribe(disposable -> progress.setValue(true))
            .doFinally(() -> progress.setValue(false))
            .toSortedList()
            .subscribe(ordersList -> ordersLiveData.setValue(ordersList),
                throwable -> error.setValue(ErrorUtils.getMessage(throwable))));

        return ordersLiveData;
    }

    protected LiveData<Boolean> getProgress() {
        return progress;
    }

    protected LiveData<String> getError() {
        return error;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        compositeDisposable.dispose();
    }

}