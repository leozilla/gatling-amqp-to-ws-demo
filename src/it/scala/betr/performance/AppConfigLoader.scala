package betr.performance

import com.typesafe.config.{ConfigFactory, ConfigParseOptions}
import pureconfig.{ConfigReader, Derivation}

import scala.reflect.ClassTag

object AppConfigLoader {

  def load[C: ClassTag](namespace: String)(implicit reader: Derivation[ConfigReader[C]]): C = {
    val classLoader = getClass.getClassLoader

    val parseOptions = ConfigParseOptions.defaults().setAllowMissing(false)
    val config       = ConfigFactory.parseResources(classLoader, "application.conf", parseOptions).resolve()
    pureconfig.loadConfigOrThrow[C](config, s"betr.performance.$namespace")
  }
}
