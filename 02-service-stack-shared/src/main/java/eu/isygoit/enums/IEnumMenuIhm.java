package eu.isygoit.enums;

/**
 * The interface Enum menu ihm.
 */
public interface IEnumMenuIhm {

    /**
     * The interface Menu.
     *
     * @param <E> the type parameter
     */
    interface IMenu<E extends IRootMenu> extends IEnumType<IMenu> {

        /**
         * Gets code.
         *
         * @return the code
         */
        Long getCode();

        /**
         * Gets description.
         *
         * @return the description
         */
        String getDescription();

        /**
         * Gets icon.
         *
         * @return the icon
         */
        String getIcon();

        /**
         * Gets name.
         *
         * @return the name
         */
        String getName();

        /**
         * Gets root menu.
         *
         * @return the root menu
         */
        E getRootMenu();

        /**
         * Gets url.
         *
         * @return the url
         */
        String getUrl();
    }

    /**
     * The interface Root menu.
     */
    interface IRootMenu extends IEnumType<IRootMenu> {

        /**
         * Gets code.
         *
         * @return the code
         */
        Long getCode();

        /**
         * Gets description.
         *
         * @return the description
         */
        String getDescription();

        /**
         * Gets name.
         *
         * @return the name
         */
        String getName();

        /**
         * Gets icon.
         *
         * @return the icon
         */
        String getIcon();

        /**
         * Gets url.
         *
         * @return the url
         */
        String getUrl();
    }
}
