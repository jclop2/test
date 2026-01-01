package com.fathzer.pcloud;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

import com.pcloud.sdk.ApiClient;
import com.pcloud.sdk.ApiError;
import com.pcloud.sdk.Authenticator;
import com.pcloud.sdk.Authenticators;
import com.pcloud.sdk.DataSource;
import com.pcloud.sdk.PCloudSdk;
import com.pcloud.sdk.RemoteFolder;

@EnabledIfSystemProperty(named = "pcloud.token", matches = ".+")
class PCloudSdkTest {

    private ApiClient apiClient;
    private RemoteFolder testFolder;

    @BeforeEach
    void setup() throws IOException, ApiError {
        final Authenticator authenticator = Authenticators.newOAuthAuthenticator(System.getProperty("pcloud.token"));
        apiClient = PCloudSdk.newClientBuilder()
                .authenticator(authenticator)
                .create();
        try {
            // Check authentication
            apiClient.getUserInfo().execute();
        } catch (IOException | ApiError e) {
            apiClient.shutdown();
            throw e;
        }
        testFolder = apiClient.createFolder("/pCloudDeleteFolderTest" + System.currentTimeMillis()).execute();
    }

    @AfterEach
    void cleanup() throws IOException, ApiError {
        apiClient.deleteFolder(testFolder.folderId(), true).execute();
        apiClient.shutdown();
    }

    @Test
    void testDeleteFolder() throws ApiError, IOException {
        // Given
        RemoteFolder parent = apiClient.createFolder(testFolder,"parent").execute();
        apiClient.createFile(parent, "file1.txt", DataSource.create("content".getBytes())).execute();
        apiClient.createFolder(parent,"subfolder").execute();

        assertTrue(parent.isFolder());

        // When
        assertTrue(parent.delete(true));
        
        RemoteFolder dummy = apiClient.listFolder(testFolder.folderId(), false).execute();
        

        // Then
        assertFalse(dummy.children().stream().anyMatch(e -> e.name().equals("parent")), "Folder should not be in testFolder childrens after deletion");
//        
//        Entry afterDelete = provider.get("/parent");
//        assertFalse(afterDelete.exists(), "Folder should not exist after deletion");
//
//        Entry childAfterDelete = provider.get("/parent/file1.txt");
//        assertFalse(childAfterDelete.exists(), "Child file should not exist after parent deletion");
//
//        Entry subfolderAfterDelete = provider.get("/parent/subfolder");
//        assertFalse(subfolderAfterDelete.exists(), "Subfolder should not exist after parent deletion");
//
//        // Check that folder can be deleted twice
//        assertDoesNotThrow(parent::delete);
//
//        // That subfolder of a deleted folder can be deleted
//        assertDoesNotThrow(subfolderAfterDelete::delete);
//
//        // Check root folder can't be deleted
//        assertThrows(IOException.class, () -> root.delete(), "Should throw IOException when root folder is deleted");
    }
}
