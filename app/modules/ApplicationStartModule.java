package modules;

import com.google.inject.AbstractModule;

public class ApplicationStartModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ApplicationStartProvider.class).asEagerSingleton();
    }
}