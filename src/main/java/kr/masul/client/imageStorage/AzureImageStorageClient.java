package kr.masul.client.imageStorage;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobStorageException;
import kr.masul.system.exception.CustomBlobStorageException;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

@Service
public class AzureImageStorageClient implements ImageStorageClient{

   // pom.xml에 spring-cloud-azure-storage-blob를 추가하면 자동으로 BlobServiceClient를 생성해줌
   private final BlobServiceClient blobServiceClient;

   public AzureImageStorageClient(BlobServiceClient blobServiceClient) {
      this.blobServiceClient = blobServiceClient;
   }

   @Override
   public String uploadImage(
           String containerName, String originalImageName, InputStream data, long length)
           throws IOException {
      try {
         // conainer와 상호동작하기 위한 BlobContainerClient 객체를 받아옮
         BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
         // UUID를 활용 새로운 이미지이름을 지정
         String newImageName = UUID.randomUUID().toString() + originalImageName.substring(originalImageName.lastIndexOf("."));
         // 지정된 blob와 상호작동하는 BlobClient 객체를 받아옮.
         BlobClient blobClient = blobContainerClient.getBlobClient(newImageName);

         // blobClient를 활용해서 데이터를 업로드
         blobClient.upload(data, length, true);

         return blobClient.getBlobUrl();
      } catch (BlobStorageException e){
         throw new CustomBlobStorageException("Azure Blob Storage에 이미미 업로드 실패", e);
      }
   }
}
