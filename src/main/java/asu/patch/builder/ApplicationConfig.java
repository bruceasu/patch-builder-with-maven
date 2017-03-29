/*
 * Copyright (C) 2017 Bruce Asu<bruceasu@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom
 * the Software is furnished to do so, subject to the following conditions:
 *  　　
 * 　　The above copyright notice and this permission notice shall
 * be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES
 * OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE,
 * ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE
 * OR OTHER DEALINGS IN THE SOFTWARE.
 *
 */

package asu.patch.builder;

import java.io.File;
import java.io.Reader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.nutz.ioc.impl.PropertiesProxy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by suk on 2017/3/29.
 */
public class ApplicationConfig {
  private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationConfig.class);

  private static final String APP_HOME = System.getProperty("app.home",
      System.getProperty("user.home"));

  private static ApplicationConfig holder = new ApplicationConfig();

  private PropertiesProxy pp;

  public static ApplicationConfig getInstance() {
    return holder;
  }

  private ApplicationConfig() {
    File configDir = new File(APP_HOME, "conf");
    if (configDir.isDirectory()) {
      LOGGER.info("load {} configuration files.", configDir);
      pp = new PropertiesProxy(configDir.getAbsolutePath(),
          new File(configDir, "application.properties").getAbsolutePath());
    } else {
      pp = new PropertiesProxy("application.properties");
    }
  }

  public String getAppHome() {
    return APP_HOME;
  }
  public boolean has(String key) {
    return pp.has(key);
  }

  public PropertiesProxy set(String key, String val) {
    return pp.set(key, val);
  }

  public String check(String key) {
    return pp.check(key);
  }

  public boolean getBoolean(String key) {
    return pp.getBoolean(key);
  }

  public boolean getBoolean(String key, boolean dfval) {
    return pp.getBoolean(key, dfval);
  }

  public String get(String key, String defaultValue) {
    return pp.get(key, defaultValue);
  }

  public List<String> getList(String key) {
    return pp.getList(key);
  }

  public List<String> getList(String key, String separatorChar) {
    return pp.getList(key, separatorChar);
  }

  public String trim(String key) {
    return pp.trim(key);
  }

  public String trim(String key, String defaultValue) {
    return pp.trim(key, defaultValue);
  }

  public int getInt(String key) {
    return pp.getInt(key);
  }

  public int getInt(String key, int defaultValue) {
    return pp.getInt(key, defaultValue);
  }

  public long getLong(String key) {
    return pp.getLong(key);
  }

  public long getLong(String key, long dfval) {
    return pp.getLong(key, dfval);
  }

  public String getTrim(String key) {
    return pp.getTrim(key);
  }

  public String getTrim(String key, String defaultValue) {
    return pp.getTrim(key, defaultValue);
  }

  public List<String> getKeys() {
    return pp.getKeys();
  }

  public Collection<String> getValues() {
    return pp.getValues();
  }

  public Properties toProperties() {
    return pp.toProperties();
  }

  public PropertiesProxy joinByKey(String key) {
    return pp.joinByKey(key);
  }

  public PropertiesProxy joinAndClose(Reader r) {
    return pp.joinAndClose(r);
  }

  public Map<String, String> toMap() {
    return pp.toMap();
  }

  public String get(String key) {
    return pp.get(key);
  }

  public <T> T make(Class<T> klass, String prefix) {
    return pp.make(klass, prefix);
  }
}
