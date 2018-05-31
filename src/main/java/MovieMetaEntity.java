import java.util.Objects;

public class MovieMetaEntity {
    String title;
    String url;

    public MovieMetaEntity() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MovieMetaEntity that = (MovieMetaEntity) o;
        return Objects.equals(title, that.title) &&
                Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {

        return Objects.hash(title, url);
    }

    @Override
    public String toString() {
        return "MovieMetaEntity{" +
                "title='" + title + '\'' +
                ", url='" + url + '\'' +
                '}';
    }
}
