# Login 
gcloud auth login
# Create compute engine instance with de 'containers.yml' descriptor
gcloud compute instances create es1 --image projects/google-containers/global/images/container-vm-v20140522 --metadata-from-file google-container-manifest=containers.yml --zone europe-west1-b --machine-type n1-standard-1


# SSH connection
gcutil --service_version="v1" --project="test-elasticsearch" ssh --zone="europe-west1-b" "es1"
# Run the docker
sudo docker run -d -p 9200:9200 -p 9300:9300 dockerfile/elasticsearch

# Install nginx
sudo apt-get update
sudo apt-get install nginx
sudo /etc/init.d/nginx start


# Connect with SFTP using the SSH private key commonly ~/.ssh/google_compute_engine