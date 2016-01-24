package apps.startup;

import apps.StartupModel;
import java.awt.Component;

/**
 * A factory for {@link apps.StartupModel}s.
 *
 * StartupModelFactories are loaded via the Java {@link java.util.ServiceLoader}
 * mechanism to allow the actions JMRI can take on startup to be extended within
 * an external JAR.
 *
 * @author Randall Wood
 */
public interface StartupModelFactory {

    /**
     * Get the {@link java.lang.Class} that is generated by this factory. The
     * Class must be a subclass of {@link apps.StartupModel}.
     *
     * @return The class this factory generates.
     */
    public Class<? extends StartupModel> getModelClass();

    /**
     * Get the description for models this factory generates. This is used in
     * the preferences UI to describe the class, so it should be short.
     *
     * @return A short description.
     */
    public String getDescription();

    /**
     * Get the action text for models this factory generates. This is used in
     * menus and UI elements that start the process of creating and editing a
     * new startup action.
     *
     * If the startup action is not configurable, this should be the same as
     * {@link #getDescription()}.
     *
     * @return Action text
     */
    public String getActionText();

    /**
     * Create a new instance of the model.
     *
     * @return the new instance
     */
    public StartupModel newModel();

    /**
     * Allow user to edit the model.
     *
     * @param model  the model to edit
     * @param parent the parent component for the editing UI
     */
    public void editModel(StartupModel model, Component parent);
}
