#!/bin/bash
#
# Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
PROXY_HOST=myproxy.example.com
PROXY_PORT=8080
NON_PROXY_HOSTS="localhost|127.0.0.1|*.example.com"

JVM_OPTS=-Xms512m -Xmx4g
JAVA_OPTS=-Dhttp.proxyHost=%PROXY_HOST% -Dhttp.proxyPort=%PROXY_PORT% -Dhttp.nonProxyHosts=%NON_PROXY_HOSTS%

java $JVM_OPTS $JAVA_OPTS -jar ota2-diff-utility.jar "$@"