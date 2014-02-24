package edu.harvard.iq.dataverse;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;
import org.hibernate.validator.constraints.NotBlank;

/**
 *
 * @author gdurand
 */
@Entity
public class DataFile extends DvObject {
    private static final long serialVersionUID = 1L;
    
    @NotBlank
    private String name;
    
    @NotBlank    
    private String contentType;
    
    private String fileSystemName;

    /*
        Tabular (formerly "subsettable") data files have DataTable objects
        associated with them:
    */
    
    @OneToMany(mappedBy = "dataFile", cascade = {CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST})
    private List<DataTable> dataTables;
    
    @OneToMany(mappedBy="dataFile", cascade={CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST})
    private List<FileMetadata> fileMetadatas;
    
    @OneToMany(mappedBy="dataFile", cascade={CascadeType.REMOVE, CascadeType.MERGE, CascadeType.PERSIST})
    private List<FileMetadataFieldValue> fileMetadataFieldValues;

    public DataFile() {
        this.fileMetadatas = new ArrayList<>();
        fileMetadataFieldValues = new ArrayList<>();
    }    

    public DataFile(String name, String contentType) {
        this.name = name;
        this.contentType = contentType;
        this.fileMetadatas = new ArrayList<>();
        fileMetadataFieldValues = new ArrayList<>();
    }    

    public List<DataTable> getDataTables() {
        return dataTables;
    }

    public void setDataTables(List<DataTable> dataTables) {
        this.dataTables = dataTables;
    }
    
    public List<FileMetadata> getFileMetadatas() {
        return fileMetadatas;
    }

    public void setFileMetadatas(List<FileMetadata> fileMetadatas) {
        this.fileMetadatas = fileMetadatas;
    }
    
    public DataTable getDataTable() {
        if ( getDataTables() != null && getDataTables().size() > 0 ) {
            return getDataTables().get(0);
        } else {
            return null;
        }
    }

    public void setDataTable(DataTable dt) {
        if (this.getDataTables() == null) {
            this.setDataTables( new ArrayList() );
        } else {
            this.getDataTables().clear();
        }

        this.getDataTables().add(dt);
    }
    
    public boolean isTabularData() {
        if ( getDataTables() != null && getDataTables().size() > 0 ) {
            return true; 
        }
        return false; 
    }
    

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public Dataset getOwner() {
        return (Dataset) super.getOwner();
    }

    public void setOwner(Dataset dataset) {
        super.setOwner(dataset);
    }
    
    public String getFileSystemName() {
        return this.fileSystemName;
    }

    public void setFileSystemName(String fileSystemName) {
        this.fileSystemName = fileSystemName;
    }
    
    public String getDescription() {
        FileMetadata fmd = getLatestFileMetadata();
        
        if (fmd == null) {
            return null;
        }
        return fmd.getDescription();
    }

    public void setDescription(String description) {
        FileMetadata fmd = getLatestFileMetadata();
        
        if (fmd != null) {
            fmd.setDescription(description);
        }
    }
    
    public List<FileMetadataFieldValue> getFileMetadataFieldValues() {
        return fileMetadataFieldValues;
    }

    public void setFileMetadataFieldValues(List<FileMetadataFieldValue> fileMetadataFieldValues) {
        this.fileMetadataFieldValues = fileMetadataFieldValues;
    }
    
    public FileMetadata getFileMetadata() {
        return getLatestFileMetadata();
    }
    
    private FileMetadata getLatestFileMetadata() {
        FileMetadata fmd = null;

        for (FileMetadata fileMetadata : fileMetadatas) {
            if (fmd == null || fileMetadata.getDatasetVersion().getVersionNumber().compareTo( fmd.getDatasetVersion().getVersionNumber() ) > 0 ) {
                fmd = fileMetadata;
            }                       
        }
        return fmd;
    }
    
    public Path getFileSystemLocation() {
        String studyDirectory = this.getOwner().getFileSystemDirectory().toString();
        return Paths.get(studyDirectory, this.name);
    }
    
    public boolean isImage() {
        return (contentType != null && contentType.startsWith("image/"));
    }
    
    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof DataFile)) {
            return false;
        }
        DataFile other = (DataFile) object;
        return Objects.equals(getId(), other.getId());
    }

    @Override
    protected String toStringExtras() {
        return "name:" + getName();
    }
	
	@Override
	public void accept( Visitor v ) {
		v.visit(this);
	}
}
