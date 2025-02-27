/*
 * Copyright 2021 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openrewrite.properties;

import org.junit.jupiter.api.Test;
import org.openrewrite.Issue;
import org.openrewrite.test.RewriteTest;

import static org.openrewrite.properties.Assertions.properties;

@SuppressWarnings("UnusedProperty")
class Issue1020Test implements RewriteTest {

    @Issue("https://github.com/openrewrite/rewrite/issues/1020")
    @Test
    void removalOfDoublePound() {
        rewriteRun(
          spec -> spec.recipe(new ChangePropertyKey("server.port", "chassis.name", null, null, null)),
          properties(
            """
              key=**##**chassis.management.metrics.export.cloudwatch.awsAccessKey
              """
          )
        );
    }

    @Issue("https://github.com/openrewrite/rewrite/issues/1020")
    @Test
    void removalOfSlashPound() {
        rewriteRun(
          spec -> spec.recipe(new ChangePropertyValue("server.tomcat.accesslog.enabled", "true", null, false, null, null)),
          properties(
            """
              boot.features=https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle**/#**boot-features-jersey
              server.tomcat.accesslog.enabled=true
              """
          )
        );
    }
}
