/*
 * File created on June 11, 2014 
 *
 * Copyright 2007-2014 Carl Harris, Jr.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.soulwing.cas.undertow;

import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.servlet.ServletExtension;
import io.undertow.servlet.api.DeploymentInfo;

import javax.servlet.ServletContext;

import org.jboss.msc.inject.Injector;
import org.jboss.msc.service.AbstractService;
import org.soulwing.cas.service.AuthenticationService;

public class CasServletExtension extends AbstractService<ServletExtension> 
    implements ServletExtension {

  private final CasAuthenticationService authenticationService =
      new CasAuthenticationService();
  
  public Injector<AuthenticationService> getAuthenticationServiceInjector() {
    return authenticationService.getServiceInjector();
  }

  @Override
  public void handleDeployment(DeploymentInfo deploymentInfo, 
      ServletContext servletContext) {
    
    deploymentInfo.clearLoginMethods();
    deploymentInfo.addFirstAuthenticationMechanism(
        CasAuthenticationMechanism.MECHANISM_NAME, 
        new CasAuthenticationMechanism(deploymentInfo.getContextPath(), authenticationService));

    deploymentInfo.addInitialHandlerChainWrapper(new HandlerWrapper() {
      @Override
      public HttpHandler wrap(HttpHandler handler) {
        return new ProxyCallbackHttpHandler(handler, authenticationService);
      } 
    });

    deploymentInfo.addInnerHandlerChainWrapper(new HandlerWrapper() {
      @Override
      public HttpHandler wrap(HttpHandler handler) {
        return new PostAuthRedirectHttpHandler(handler);
      } 
    });
    
  }

}
