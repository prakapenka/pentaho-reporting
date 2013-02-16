package org.pentaho.reporting.ui.datasources.dynamic;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.pentaho.reporting.engine.classic.core.ReportDataFactoryException;
import org.pentaho.reporting.engine.classic.core.designtime.DesignTimeContext;
import org.pentaho.reporting.engine.classic.core.designtime.DesignTimeUtil;
import org.pentaho.reporting.libraries.base.util.IOUtils;
import org.pentaho.reporting.libraries.docbundle.BundleUtilities;
import org.pentaho.reporting.libraries.docbundle.WriteableDocumentBundle;

public class TransformationResource {

  private static final String MIMETYPE = "text/xml";


  private final String resourceEntryName;

  private final WriteableDocumentBundle bundle;

  private InputStream in;

  public TransformationResource(DesignTimeContext context, String entryKey) {

    resourceEntryName = entryKey;

    bundle = (WriteableDocumentBundle) DesignTimeUtil.getMasterReport(context.getReport()).getBundle();
  }

  public void write() throws ReportDataFactoryException {

    final InputStream fin = getInputStream();
    try {
      if (exists()) {
        remove();
      }

      final OutputStream outputStream = bundle.createEntry(resourceEntryName, MIMETYPE);
      try {

        IOUtils.getInstance().copyStreams(fin, outputStream);
      } finally {
        
        outputStream.close();
      }

      bundle.getWriteableDocumentMetaData().setEntryAttribute(resourceEntryName, BundleUtilities.STICKY_FLAG, "true"); // NON-NLS
      
    }catch(IOException e){
      throw new ReportDataFactoryException("", e);
    }
    
  }

  public boolean exists() {

    return bundle.isEntryExists(resourceEntryName);
  }

  public void remove() throws ReportDataFactoryException{

    try {

      bundle.removeEntry(resourceEntryName);
    
    } catch (IOException e) {
      
      throw new ReportDataFactoryException("", e);
    }
  }

  public InputStream getInputStream() {
    return in;
  }

  public void setInputStream(InputStream in) {
    this.in = in;
  }
}
