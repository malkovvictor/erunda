package ru.heritagepw.android.axolotl;

public class CityPhoto {
    String filename;
    String copyrightLink;
    String copyrightAuthor;
    String copyrightLicense;
    String name;

    public CityPhoto(String filename, String name, String copyrightLink, String copyrightAuthor, String copyrightLicense) {
        this.filename = filename;
        this.copyrightLink = copyrightLink;
        this.copyrightAuthor = copyrightAuthor;
        this.copyrightLicense = copyrightLicense;
        this.name = name;
    }

    public String getCopyright() {
        return String.format("<a href=\"%s\">Image</a> by %s / %s", copyrightLink, copyrightAuthor, copyrightLicense);
    }

    public String getFilename() {
        return String.format("poi/%s", filename);
    }
}
