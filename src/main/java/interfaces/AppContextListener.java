package interfaces;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

@WebListener
public class AppContextListener implements ServletContextListener {

  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    AdmConexion.INSTANCE.cerrarPool();
  }
}