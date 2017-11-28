package dimdoors.common.watcher;

import com.google.common.collect.Lists;

import java.util.List;

public class UpdateWatcherProxy<T> implements IUpdateWatcher<T> {

	private List<IUpdateWatcher<T>> watchers;

	public UpdateWatcherProxy() {
		watchers = Lists.newArrayList();
	}

	@Override
	public void onCreated(T message) {
		for (IUpdateWatcher<T> receiver : watchers) {
			receiver.onCreated(message);
		}
	}

	@Override
	public void onDeleted(T message) {
		for (IUpdateWatcher<T> receiver : watchers) {
			receiver.onDeleted(message);
		}
	}

	public void registerReceiver(IUpdateWatcher<T> receiver) {
		watchers.add(receiver);
	}

	public boolean unregisterReceiver(IUpdateWatcher<T> receiver) {
		return watchers.remove(receiver);
	}

	@Override
	public void update(T message) {
		for (IUpdateWatcher<T> receiver : watchers) {
			receiver.update(message);
		}
	}
}
