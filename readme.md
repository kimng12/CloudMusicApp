# CloudMusicApp
Readme containing instructions, requirements and contributions

## <center>Requirements</center>

### Linix (ubuntu)
&emsp;**Java setup**
<div style="margin-left: 20px;">
Verify install
<pre style="margin-left: 20px;">
java --version
</pre>
Java instalation (if you the above command finds nothing)
<pre style="margin-left: 20px;">
sudo apt install default-jre
</pre>
</div>

&emsp;**Maven setup**
<div style="margin-left: 20px;">
Verify install
<pre style="margin-left: 20px;">
mvn -v
</pre>
Maven instalation (if you the above command finds nothing)
<pre style="margin-left: 20px;">
sudo apt install maven
</pre>
</div>


## <center>Usage</center>

### Step 1: Aws Credentials
<div style="margin-left: 20px;">
In order to run this locally you will need to provide the <b>Access Key</b>, <b>Secret Access Key</b> and the <b>Session Token</b>.

> **NOTE:** You do not have to do this step if you have used aws configure or manually placed your **Access Key**, **Secret Access Key** and **Session Token** within the `.aws/credentials` file (usually located in the home directory).

Once you have obtained your **Access Key**, **Secret Access Key** and **Session Token**, place it within the [aws-credentials.properties](src/main/resources/aws-credentials.properties) file.
</div>

### Step 2-1: Running Website (Locally)
<pre style="margin-left: 20px;">
mvn jetty:run
</pre>

### Step 2-2: build package
<pre style="margin-left: 20px;">
mvn clean package
</pre>
<div style="margin-left: 20px;">
Once the above commabnd has been run you should be able to find a <b>.war</b> file located within the <a href="target/">target</a> folder
</div>