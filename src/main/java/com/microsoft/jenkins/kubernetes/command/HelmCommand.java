/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.jenkins.kubernetes.command;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.microsoft.jenkins.kubernetes.CustomerTiller;
import com.microsoft.jenkins.kubernetes.credentials.KubeconfigCredentials;
import hudson.model.Item;
import hudson.security.ACL;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import org.apache.commons.lang3.StringUtils;
import org.microbean.helm.ReleaseManager;
import org.microbean.helm.Tiller;

import java.io.IOException;
import java.util.Collections;

public abstract class HelmCommand {
    public String getKubeConfigContent(String configId, Item owner) {
        if (StringUtils.isNotBlank(configId)) {
            final KubeconfigCredentials credentials = CredentialsMatchers.firstOrNull(
                    CredentialsProvider.lookupCredentials(
                            KubeconfigCredentials.class,
                            owner,
                            ACL.SYSTEM,
                            Collections.<DomainRequirement>emptyList()),
                    CredentialsMatchers.withId(configId));
            if (credentials == null) {
                throw new IllegalArgumentException("Cannot find kubeconfig credentials with id " + configId);
            }
            return credentials.getContent();
        }
        return null;
    }

    public ReleaseManager getReleaseManager(String kubeConfig, String tillerNamespace) throws IOException {
        try (
                final DefaultKubernetesClient client = new DefaultKubernetesClient(Config.fromKubeconfig(kubeConfig));
                final Tiller tiller = new CustomerTiller(client, tillerNamespace);
                final ReleaseManager releaseManager = new ReleaseManager(tiller)) {
            return releaseManager;
        }
    }
}