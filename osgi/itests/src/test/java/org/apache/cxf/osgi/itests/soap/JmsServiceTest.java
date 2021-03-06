/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.cxf.osgi.itests.soap;

import java.io.InputStream;
import java.util.Collections;

import javax.jms.ConnectionFactory;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.osgi.itests.CXFOSGiTestSupport;
import org.apache.cxf.transport.jms.ConnectionFactoryFeature;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.ops4j.pax.tinybundles.core.TinyBundles;
import org.osgi.framework.Constants;

import static org.ops4j.pax.exam.CoreOptions.provision;
import static org.ops4j.pax.exam.karaf.options.KarafDistributionOption.features;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class JmsServiceTest extends CXFOSGiTestSupport {

    @Test
    public void testJmsEndpoint() throws Exception {
        Greeter greeter = greeterJms();
        String res = greeter.greetMe("Chris");
        Assert.assertEquals("Hi Chris", res);
    }

    private Greeter greeterJms() {
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        factory.setServiceClass(Greeter.class);
        factory.setAddress("jms:queue:greeter");
        ConnectionFactory connectionFactory = createConnectionFactory();
        factory.setFeatures(Collections.singletonList(new ConnectionFactoryFeature(connectionFactory)));
        return factory.create(Greeter.class);
    }

    private ActiveMQConnectionFactory createConnectionFactory() {
        ActiveMQConnectionFactory connectionFactory
            = new ActiveMQConnectionFactory("vm://JmsServiceTest");
        connectionFactory.setUserName("karaf");
        connectionFactory.setPassword("karaf");
        return connectionFactory;
    }

    @Configuration
    public Option[] config() {
        return new Option[] {
            cxfBaseConfig(),
            testUtils(),
            features(cxfUrl, "cxf-core", "cxf-jaxws", "cxf-transports-jms"),
            features(amqUrl, "aries-blueprint", "shell-compat", "activemq-broker-noweb"),
            provision(serviceBundle())
        };
    }

    private InputStream serviceBundle() {
        return TinyBundles.bundle().add(JmsTestActivator.class).add(Greeter.class).add(GreeterImpl.class)
            .set(Constants.BUNDLE_ACTIVATOR, JmsTestActivator.class.getName()).build(TinyBundles.withBnd());
    }

}
