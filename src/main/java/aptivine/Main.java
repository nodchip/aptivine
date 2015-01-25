package aptivine;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.common.base.Preconditions;
import com.google.inject.Guice;
import com.google.inject.Inject;

public class Main {

  private static final Logger logger = Logger.getLogger(Main.class.toString());

  private final Controller controller;

  @Inject
  public Main(Controller controller) {
    this.controller = Preconditions.checkNotNull(controller);
  }

  private void run() {
    controller.start();
  }

  public static void main(String[] args) {
    Logger.getGlobal().setLevel(Level.FINE);

    Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
      @Override
      public void uncaughtException(Thread t, Throwable e) {
        logger.log(Level.SEVERE, "UncaughtException: t=" + t, e);
        System.exit(-1);
      }
    });

    Guice.createInjector(new AptivineModule()).getInstance(Main.class).run();
  }
}
