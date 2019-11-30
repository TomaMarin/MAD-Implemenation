
import java.util.Objects;

public class DataRow {

    private String gender;
    private String ethnicity;
    private String levelOfEducation;
    private String lunch;
    private boolean preparedForTest;
    private int mathScore;
    private int readingScore;
    private int writingScore;

    public DataRow(String gender, String ethnicity, String levelOfEducation, String lunch, boolean preparedForTest, int mathScore, int readingScore, int writingScore) {
        this.gender = gender;
        this.ethnicity = ethnicity;
        this.levelOfEducation = levelOfEducation;
        this.lunch = lunch;
        this.preparedForTest = preparedForTest;
        this.mathScore = mathScore;
        this.readingScore = readingScore;
        this.writingScore = writingScore;
    }

    public DataRow() {
    }

    public DataRow(int mathScore, int readingScore, int writingScore) {
        this.mathScore = mathScore;
        this.readingScore = readingScore;
        this.writingScore = writingScore;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public String getLevelOfEducation() {
        return levelOfEducation;
    }

    public void setLevelOfEducation(String levelOfEducation) {
        this.levelOfEducation = levelOfEducation;
    }

    public String getLunch() {
        return lunch;
    }

    public void setLunch(String lunch) {
        this.lunch = lunch;
    }

    public boolean isPreparedForTest() {
        return preparedForTest;
    }

    public void setPreparedForTest(boolean preparedForTest) {
        this.preparedForTest = preparedForTest;
    }

    public int getMathScore() {
        return mathScore;
    }

    public void setMathScore(int mathScore) {
        this.mathScore = mathScore;
    }

    public int getReadingScore() {
        return readingScore;
    }

    public void setReadingScore(int readingScore) {
        this.readingScore = readingScore;
    }

    public int getWritingScore() {
        return writingScore;
    }

    public void setWritingScore(int writingScore) {
        this.writingScore = writingScore;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataRow dataRow = (DataRow) o;
        return preparedForTest == dataRow.preparedForTest &&
                mathScore == dataRow.mathScore &&
                readingScore == dataRow.readingScore &&
                writingScore == dataRow.writingScore &&
                Objects.equals(gender, dataRow.gender) &&
                Objects.equals(ethnicity, dataRow.ethnicity) &&
                Objects.equals(levelOfEducation, dataRow.levelOfEducation) &&
                Objects.equals(lunch, dataRow.lunch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gender, ethnicity, levelOfEducation, lunch, preparedForTest, mathScore, readingScore, writingScore);
    }

    @Override
    public String toString() {
        return "DataRow{" +
                "gender='" + gender + '\'' +
                ", ethnicity='" + ethnicity + '\'' +
                ", levelOfEducation='" + levelOfEducation + '\'' +
                ", lunch='" + lunch + '\'' +
                ", preparedForTest=" + preparedForTest +
                ", mathScore=" + mathScore +
                ", readingScore=" + readingScore +
                ", writingScore=" + writingScore +
                '}'+"\n";
    }
}
