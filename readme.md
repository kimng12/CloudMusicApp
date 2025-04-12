# CloudMusicApp
Readme containing instructions, requirements and contributions

---

## <center>Requirements</center>

### Linix (Ubuntu)

&emsp;**Java setup**  
<div style="margin-left: 20px;">
Verify install
<pre style="margin-left: 20px;">
java --version
</pre>
Java installation (if the above command fails)
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
Maven installation (if the above command fails)
<pre style="margin-left: 20px;">
sudo apt install maven
</pre>
</div>

---

## <center>Usage</center>

### Step 1: AWS Credentials

<div style="margin-left: 20px;">
In order to run this locally you will need to provide the <b>Access Key</b>, <b>Secret Access Key</b> and the <b>Session Token</b>.

> <b>NOTE:</b> You do not have to do this step if you have used <code>aws configure</code> or manually placed your credentials inside the <code>.aws/credentials</code> file (usually located in your home directory).

Once you have obtained your credentials, place them in: <code>src/main/resources/aws-credentials.properties</code> like this:
<pre style="margin-left: 20px;">
aws_access_key_id=YOUR_KEY
aws_secret_access_key=YOUR_SECRET
aws_session_token=YOUR_SESSION_TOKEN
</pre>
</div>

---

### Step 2-1: Running Website (Locally)

<pre style="margin-left: 20px;">
mvn jetty:run
</pre>

Go to [http://localhost:8080](http://localhost:8080)

---

### Step 2-2: Build WAR package

<pre style="margin-left: 20px;">
mvn clean package
</pre>

<div style="margin-left: 20px;">
After running the above command, you'll find the <b>ROOT.war</b> file inside the <a href="target/">target</a> folder.
</div>

---

## <center>Deployment on EC2</center>

### Step 3: Upload to EC2

<pre style="margin-left: 20px;">
scp -i your-key.pem target/ROOT.war ubuntu@your-ec2-public-dns:/tmp/
</pre>

### Step 4: Move to Tomcat Webapps

<pre style="margin-left: 20px;">
sudo mv /tmp/ROOT.war /opt/tomcat9/webapps/
</pre>

### Step 5: Restart Tomcat

<pre style="margin-left: 20px;">
sudo systemctl restart tomcat9
</pre>

### Step 6: Setup Apache2 Proxy (port 80)

Edit the file `/etc/apache2/sites-available/000-default.conf`:

<pre style="margin-left: 20px;">
&lt;VirtualHost *:80&gt;
    ServerAdmin webmaster@localhost
    ServerName your-ec2-public-dns

    ProxyPreserveHost On
    ProxyPass / http://localhost:8080/
    ProxyPassReverse / http://localhost:8080/

    ErrorLog ${APACHE_LOG_DIR}/error.log
    CustomLog ${APACHE_LOG_DIR}/access.log combined
&lt;/VirtualHost&gt;
</pre>

Enable the required modules:

<pre style="margin-left: 20px;">
sudo a2enmod proxy proxy_http
sudo systemctl restart apache2
</pre>

Now your app should be accessible at:  
**http://your-ec2-public-dns**

---

## <center>Lambda + API Gateway Integration</center>

This app uses one Lambda function behind an API Gateway to handle:

- register
- login
- subscribe
- unsubscribe
- search

Send a `POST` request with a `call` field to determine the action:

<pre style="margin-left: 20px;">
{
  "call": "register",
  "email": "test@example.com",
  "password": "1234",
  "user_name": "test"
}
</pre>

Enable **CORS** on all API Gateway endpoints to allow access from your web frontend.

---

## <center>Frontend Files</center>

The key HTML files in the project:

- `index.html` – Home screen
- `login.html` – Login page
- `register.html` – Register new user
- `main.html` – Music interface (query, subscribe, manage)
- `logout.html` – Logout handler

These live in both:

- `src/main/webapp/` – For local testing (Jetty)
- `webapp/` – For EC2 deployment (WAR build)

---

## <center>Contributors</center>

- s3970589 – Kim Nguyen
- s4027383 – Sung Gyu Lee
- s3946837 – Janssen Cabanada
- S3948643 – Philip Goutama
---
