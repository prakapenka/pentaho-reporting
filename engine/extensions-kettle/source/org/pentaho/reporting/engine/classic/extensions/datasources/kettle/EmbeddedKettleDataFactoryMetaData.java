package org.pentaho.reporting.engine.classic.extensions.datasources.kettle;

import java.util.Locale;

import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.designtime.DataSourcePlugin;
import org.pentaho.reporting.engine.classic.core.metadata.DefaultDataFactoryMetaData;

public class EmbeddedKettleDataFactoryMetaData extends DefaultDataFactoryMetaData
{
  private String displayName;
  private String pluginId;

  /**
   * Create a new metadata object for the embedded datafactory.
   * @param name the unique identifier, probably just the file name of the template KTR file
   * @param displayName the display name. Could be the file name as well, or something totally different. Probably
   *                    needs to be internationalized in the production code.
   * @param pluginId the plugin id might not be needed at all. Just see it as a placeholder in case you
   *                 want to pass more context information around. Remove it, if it is not needed at all.
   */
  public EmbeddedKettleDataFactoryMetaData(final String name, final String displayName, final String pluginId)
  {
    super(name, "org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleDataFactoryBundle",
        "",
        false, // expert
        false, // preferred
        false, // hidden
        false, // deprecated,
        true,  // editable
        false, // free-form
        false, // metadata-source
        false, // experimental
        new KettleDataFactoryCore(),
        ClassicEngineBoot.computeVersionId(4, 0, 0));

    this.displayName = displayName;
    this.pluginId = pluginId;
  }

  public String getDisplayName(final Locale locale)
  {
    return displayName;
  }

  public String getDescription(final Locale locale)
  {
    return displayName;
  }

  public DataSourcePlugin createEditor()
  {
    final DataSourcePlugin editor = super.createEditor();
    if (editor instanceof EmbeddedKettleDataFactoryEditor == false)
    {
      throw new IllegalStateException(String.valueOf(editor));
    }

    final EmbeddedKettleDataFactoryEditor dataFactoryEditor = (EmbeddedKettleDataFactoryEditor) editor;
    dataFactoryEditor.configure(getName(), pluginId);
    return editor;
  }

  protected String getEditorConfigurationKey()
  {
    return "org.pentaho.reporting.engine.classic.metadata.datafactory-editor.org.pentaho.reporting.engine.classic.extensions.datasources.kettle.KettleDataFactory:EmbeddedTransformationDataSourcePlugin";
  }
}
