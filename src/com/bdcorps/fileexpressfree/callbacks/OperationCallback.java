package com.bdcorps.fileexpressfree.callbacks;

public interface OperationCallback<T> {

	T onSuccess();

	void onFailure(Throwable e);
}
