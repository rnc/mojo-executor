/*
 * Copyright 2013 Robert Munteanu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.twdata.maven.mojoexecutor;

import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.InvalidPluginDescriptorException;
import org.apache.maven.plugin.PluginDescriptorParsingException;
import org.apache.maven.plugin.PluginNotFoundException;
import org.apache.maven.plugin.PluginResolutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.sonatype.aether.repository.RemoteRepository;
import org.twdata.maven.mojoexecutor.MojoExecutor.ExecutionEnvironment;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * The <tt>MavenCompatibilityHelper</tt> hides incompatibilities between Maven versions
 *
 */
public class MavenCompatibilityHelper {
    private static Method getRepositorySession;
    private static Method loadPlugin;

    static {

        for (Method m : MavenSession.class.getMethods()) {
            if ("getRepositorySession".equals(m.getName())) {
                getRepositorySession = m;
                break;
            }
        }

        if (getRepositorySession == null) {
            throw new ExceptionInInitializerError("Unable to locate getRepositorySession method");
        }

        for (Method m : BuildPluginManager.class.getMethods()) {
            if ("loadPlugin".equals(m.getName())) {
                loadPlugin = m;
                break;
            }
        }

        if (loadPlugin == null) {
            throw new ExceptionInInitializerError("Unable to locate loadPluginDescriptor method");
        }
    }

    public static PluginDescriptor loadPluginDescriptor(Plugin plugin, ExecutionEnvironment env, MavenSession session)
            throws PluginResolutionException, PluginDescriptorParsingException, InvalidPluginDescriptorException,
            PluginNotFoundException{

        try {
            Object repositorySession = getRepositorySession.invoke(session);

            BuildPluginManager pluginManager = env.getPluginManager();

            List<RemoteRepository> repositories = null;
            if ( session.getCurrentProject() != null ) {
                repositories = session.getCurrentProject().getRemotePluginRepositories();
            }
            return (PluginDescriptor) loadPlugin.invoke(pluginManager, plugin, repositories, repositorySession);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
