package net.pms.executor;


public interface Directive {

	Runnable createRunnable();
}
