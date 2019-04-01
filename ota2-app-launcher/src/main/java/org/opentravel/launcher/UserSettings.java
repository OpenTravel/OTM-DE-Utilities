/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opentravel.launcher;

import org.apache.commons.lang3.StringUtils;
import org.opentravel.application.common.AbstractUserSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

/**
 * Persists settings for the <code>App-Launcher</code> application between sessions.
 */
public class UserSettings extends AbstractUserSettings {

    private static final String USE_PROXY = "useProxy";
    private static final String PROXY_HOST = "proxyHost";
    private static final String PROXY_PORT = "proxyPort";
    private static final String NON_PROXY_HOSTS = "nonProxyHosts";
    private static final String USER_SETTINGS_FILE = "/.ota2/.al-settings.properties";

    private static File settingsFile = new File( System.getProperty( "user.home" ), USER_SETTINGS_FILE );
    private static final Logger log = LoggerFactory.getLogger( UserSettings.class );

    private boolean useProxy;
    private String proxyHost;
    private Integer proxyPort;
    private String nonProxyHosts;

    /**
     * Returns the user settings from the prior session. If no prior settings exist, default settings are returned.
     * 
     * @return UserSettings
     */
    public static UserSettings load() {
        UserSettings settings;

        if (!settingsFile.exists()) {
            settings = getDefaultSettings();

        } else {
            try (InputStream is = new FileInputStream( settingsFile )) {
                Properties usProps = new Properties();

                usProps.load( is );
                settings = new UserSettings();
                settings.load( usProps );

            } catch (Exception e) {
                log.error( "Error loading settings from prior session (using defaults).", e );
                settings = getDefaultSettings();
            }


        }
        return settings;
    }

    /**
     * @see org.opentravel.application.common.AbstractUserSettings#load(java.util.Properties)
     */
    @Override
    protected void load(Properties settingsProps) {
        String useProxyStr = settingsProps.getProperty( USE_PROXY, "false" );
        String prxHost = settingsProps.getProperty( PROXY_HOST );
        String proxyPortStr = settingsProps.getProperty( PROXY_PORT );
        String nonPrxHosts = settingsProps.getProperty( NON_PROXY_HOSTS );
        boolean usePrx = false;
        Integer prxPort = null;
    
        try {
            usePrx = Boolean.parseBoolean( useProxyStr );
            prxPort = StringUtils.isEmpty( proxyPortStr ) ? null : Integer.parseInt( proxyPortStr );
    
        } catch (NumberFormatException e) {
            // Ignore error and use default value(s)
        }
    
        setUseProxy( usePrx );
        setProxyHost( prxHost );
        setProxyPort( prxPort );
        setNonProxyHosts( nonPrxHosts );
        super.load( settingsProps );
    }

    /**
     * @see org.opentravel.application.common.AbstractUserSettings#save()
     */
    @Override
    public void save() {
        if (!settingsFile.getParentFile().exists()) {
            settingsFile.getParentFile().mkdirs();
        }
        try (OutputStream out = new FileOutputStream( settingsFile )) {
            Properties usProps = new Properties();

            save( usProps );
            usProps.store( out, null );

        } catch (IOException e) {
            log.error( "Error saving user settings.", e );
        }
    }

    /**
     * @see org.opentravel.application.common.AbstractUserSettings#save(java.util.Properties)
     */
    @Override
    protected void save(Properties settingsProps) {
        if (!StringUtils.isEmpty( proxyHost )) {
            settingsProps.put( PROXY_HOST, proxyHost );
        }
        if (proxyPort != null) {
            settingsProps.put( PROXY_PORT, proxyPort.toString() );
        }
        if (!StringUtils.isEmpty( nonProxyHosts )) {
            settingsProps.put( NON_PROXY_HOSTS, nonProxyHosts );
        }
        settingsProps.put( USE_PROXY, useProxy + "" );
        super.save( settingsProps );
    }

    /**
     * Returns the default user settings.
     * 
     * @return UserSettings
     */
    public static UserSettings getDefaultSettings() {
        UserSettings settings = new UserSettings();
        String portStr = MessageBuilder.getDefaultValue( PROXY_PORT );

        settings.setWindowPosition( settings.getDefaultWindowPosition() );
        settings.setWindowSize( settings.getDefaultWindowSize() );
        settings.setUseProxy( Boolean.parseBoolean( MessageBuilder.getDefaultValue( USE_PROXY ) ) );
        settings.setProxyHost( MessageBuilder.getDefaultValue( PROXY_HOST ) );
        settings.setProxyPort( StringUtils.isEmpty( portStr ) ? null : Integer.parseInt( portStr ) );
        settings.setNonProxyHosts( MessageBuilder.getDefaultValue( NON_PROXY_HOSTS ) );
        return settings;
    }

    /**
     * @see org.opentravel.application.common.AbstractUserSettings#getDefaultWindowSize()
     */
    @Override
    public Dimension getDefaultWindowSize() {
        return new Dimension( 500, 325 );
    }

    /**
     * Returns the flag indicating whether a network proxy is to be used.
     *
     * @return boolean
     */
    public boolean isUseProxy() {
        return useProxy;
    }

    /**
     * Assigns the flag indicating whether a network proxy is to be used.
     *
     * @param useProxy the flag value to assign
     */
    public void setUseProxy(boolean useProxy) {
        this.useProxy = useProxy;
    }

    /**
     * Returns the host name of the proxy server
     *
     * @return String
     */
    public String getProxyHost() {
        return proxyHost;
    }

    /**
     * Assigns the host name of the proxy server
     *
     * @param proxyHost the field value to assign
     */
    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    /**
     * Returns the port number of the proxy server (default is 80).
     *
     * @return Integer
     */
    public Integer getProxyPort() {
        return proxyPort;
    }

    /**
     * Assigns the port number of the proxy server (default is 80).
     *
     * @param proxyPort the field value to assign
     */
    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    /**
     * Returns the list of hosts that should be reached directly, bypassing the proxy. This is a list of patterns
     * separated by '|'. The patterns may start or end with a '*' for wildcards. Any host matching one of these patterns
     * will be reached through a direct connection instead of through a proxy.
     *
     * @return String
     */
    public String getNonProxyHosts() {
        return nonProxyHosts;
    }

    /**
     * Assigns the list of hosts that should be reached directly, bypassing the proxy. This is a list of patterns
     * separated by '|'. The patterns may start or end with a '*' for wildcards. Any host matching one of these patterns
     * will be reached through a direct connection instead of through a proxy.
     *
     * @param nonProxyHosts the field value to assign
     */
    public void setNonProxyHosts(String nonProxyHosts) {
        this.nonProxyHosts = nonProxyHosts;
    }

}
