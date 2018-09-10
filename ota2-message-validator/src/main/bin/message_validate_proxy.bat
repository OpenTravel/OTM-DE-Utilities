@REM
@REM Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
@REM
@REM Licensed under the Apache License, Version 2.0 (the "License");
@REM you may not use this file except in compliance with the License.
@REM You may obtain a copy of the License at
@REM
@REM         http://www.apache.org/licenses/LICENSE-2.0
@REM
@REM Unless required by applicable law or agreed to in writing, software
@REM distributed under the License is distributed on an "AS IS" BASIS,
@REM WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@REM See the License for the specific language governing permissions and
@REM limitations under the License.
@REM

@echo off
set PROXY_HOST=myproxy.example.com
set PROXY_PORT=8080
set NON_PROXY_HOSTS="localhost|127.0.0.1|*.example.com"

set JVM_OPTS=-Xms512m -Xmx1g
set JAVA_OPTS=-Dhttp.proxyHost=%PROXY_HOST% -Dhttp.proxyPort=%PROXY_PORT% -Dhttp.nonProxyHosts=%NON_PROXY_HOSTS%

java %JVM_OPTS% %JAVA_OPTS% -jar ota2-message-validator.jar %*