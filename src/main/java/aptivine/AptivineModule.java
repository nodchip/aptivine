package aptivine;

import aptivine.database.DatabaseModule;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.inject.AbstractModule;
import com.google.inject.Scopes;

public class AptivineModule extends AbstractModule {

  private static final String DATABASE_FILE_NAME = "database.db";

  @Override
  protected void configure() {
    install(new DatabaseModule(DATABASE_FILE_NAME));

    bind(HttpTransport.class).to(NetHttpTransport.class).in(Scopes.SINGLETON);
  }
}
