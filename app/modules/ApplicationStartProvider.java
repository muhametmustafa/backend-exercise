package modules;

import akka.actor.ActorSystem;
import akka.cluster.Cluster;
import akka.discovery.Discovery;
import akka.management.cluster.bootstrap.ClusterBootstrap;
import akka.management.javadsl.AkkaManagement;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.typesafe.config.Config;
import play.Logger;

@Singleton
public class ApplicationStartProvider {

    @Inject
    public ApplicationStartProvider(Config config, ActorSystem system) {
        String mode = config.getString("mode");
        Logger.of(this.getClass()).debug("------- Running in mode -------");
        Logger.of(this.getClass()).debug(mode);
        // Getting the runtime reference from system
        Runtime runtime = Runtime.getRuntime();
        Logger.of(this.getClass()).debug("------- Heap utilization statistics [MB] -------");
        int mb = 1024 * 1024;
        //Print total available memory
        Logger.of(this.getClass()).debug("Total Memory:" + runtime.totalMemory() / mb);
        //Print Maximum available memory
        Logger.of(this.getClass()).debug("Max Memory:" + runtime.maxMemory() / mb);

        if (mode.equals("production")) {
            Logger.of(this.getClass()).debug("loading kubernetes api service discovery");
            Discovery.get(system).loadServiceDiscovery("kubernetes-api");
        }

        String secret = config.getString("play.http.secret.key");
        Logger.of(this.getClass()).debug("Play SECRET, {}", secret);
        Logger.of(this.getClass()).debug("SYSTEM NAME: {}", system.name());
        // Akka Management hosts the HTTP routes used by bootstrap
        AkkaManagement.get(system).start();

        // Starting the bootstrap process needs to be done explicitly
        ClusterBootstrap.get(system).start();

        final Cluster cluster = Cluster.get(system);
        cluster.registerOnMemberUp(() -> {
            Logger.of(this.getClass()).debug("MEMBER IS UP");
        });
    }
}