/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.harvard.iq.dataverse;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.Size;
import org.hibernate.validator.constraints.NotBlank;
//import org.springframework.format.annotation.DateTimeFormat;

/**
 *
 * @author skraffmiller
 */
@Entity
public class Dataset extends DvObjectContainer {

    private static final long serialVersionUID = 1L;
    
    // #VALIDATION: page defines maxlength in input:textarea component
    @Size(max = 1000, message = "Description must be at most 1000 characters.")
    private String description;
    
    @OneToMany (mappedBy = "owner", cascade = CascadeType.MERGE)
    private List<DataFile> files = new ArrayList();

    
    private String protocol;
    private String authority;
    @NotBlank(message = "Please enter an identifier for your dataset.")
    private String identifier;
    
    public String getProtocol() {
        return protocol;
    }
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getAuthority() {
        return authority;
    }
    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
 
    
   public String getPersistentURL() {       
        if (this.getProtocol().equals("hdl")){
            return getHandleURL();
        } else if (this.getProtocol().equals("doi")){
            return getEZIdURL();
        } else {
            return "";
        }
    }
    
    private String getHandleURL() {
         return "http://hdl.handle.net/"+authority+"/"+getId();
    }
    
    private String getEZIdURL() {        
        return "http://dx.doi.org/"+authority+"/"+getId();
    }

    public List<DataFile> getFiles() {
        return files;
    }
    public void setFiles(List<DataFile> files) {
        this.files = files;
    }


    @OneToMany (mappedBy = "dataset")
    @OrderBy("versionNumber DESC")
    private List<DatasetVersion> versions = new ArrayList();
    
    public DatasetVersion getLatestVersion(){
        if (versions.isEmpty()){
            DatasetVersion datasetVersion = new DatasetVersion();
            //datasetVersion.setMetadata(new Metadata());
            datasetVersion.setDataset(this);
            datasetVersion.setVersionState(DatasetVersion.VersionState.DRAFT);
            datasetVersion.setVersionNumber(new Long (1));
            this.versions.add(datasetVersion);
            return datasetVersion;
        } else {
            return versions.get(0);
        }
    }
    
    public List<DatasetVersion> getVersions() {
        return versions;
    }
    public void setVersions(List<DatasetVersion> versions) {
        this.versions = versions;
    }

    private DatasetVersion createNewDatasetVersion() {
        DatasetVersion dsv = new DatasetVersion();
        dsv.setVersionState(DatasetVersion.VersionState.DRAFT);

        DatasetVersion latestVersion = getLatestVersion();
        //dsv.setMetadata(new Metadata());
        dsv.setFileMetadatas(new ArrayList());
        //dsv.getMetadata().setDatasetVersion(dsv);

       for(FileMetadata fm : latestVersion.getFileMetadatas()) {
           FileMetadata newFm = new FileMetadata();
           newFm.setCategory(fm.getCategory());
           newFm.setDescription(fm.getDescription());
           newFm.setLabel(fm.getLabel());
           newFm.setDataFile(fm.getDataFile());
           newFm.setDatasetVersion(dsv);
           dsv.getFileMetadatas().add(newFm);
       }

        dsv.setVersionNumber(latestVersion.getVersionNumber()+1);
        // I'm adding the version to the list so it will be persisted when
        // the study object is persisted.
        getVersions().add(0, dsv);
        dsv.setDataset(this);
        return dsv;
    }
    
    public DatasetVersion getEditVersion() {
        DatasetVersion latestVersion = this.getLatestVersion();
        if (!latestVersion.isWorkingCopy()) {
            // if the latest version is released or archived, create a new version for editing
            return createNewDatasetVersion();
        } else {
            // else, edit existing working copy
            return latestVersion;
        } 
    }

    public Path getFileSystemDirectory() {
        Path studyDir = null;
                
        
        String filesRootDirectory = System.getProperty("dataverse.files.directory");
        if (filesRootDirectory == null || filesRootDirectory.equals("")) {
            filesRootDirectory = "/tmp/files";
        }
        
        studyDir = Paths.get(filesRootDirectory, this.getAuthority().toString(), this.getIdentifier().toString());        
        return studyDir; 
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Dataset)) {
            return false;
        }
        Dataset other = (Dataset) object;
        return Objects.equals(getId(), other.getId());
    }

    @Override
	public void accept( Visitor v ) {
		v.visit(this);
	}
}
