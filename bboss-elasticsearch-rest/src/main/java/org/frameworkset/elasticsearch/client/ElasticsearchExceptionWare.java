package org.frameworkset.elasticsearch.client;
/**
 * Copyright 2025 bboss
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.frameworkset.spi.remote.http.proxy.ExceptionWare;
import org.frameworkset.spi.remote.http.proxy.HttpServiceHosts;

/**
 * <p>Description: </p>
 * <p></p>
 *
 * @author biaoping.yin
 * @Date 2025/2/18
 */
public class ElasticsearchExceptionWare implements ExceptionWare {
    private HttpServiceHosts httpServiceHosts;
    @Override
    public Exception getExceptionFromResponse(HttpClientResponseHandler responseHandler) {
        return ElasticSearchRestClient.getException(responseHandler);
    }

    @Override
    public void setHttpServiceHosts(HttpServiceHosts httpServiceHosts) {
        this.httpServiceHosts = httpServiceHosts;
    }
}
