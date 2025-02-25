package eu.isygoit.helper.bo;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;

import java.util.ArrayList;
import java.util.List;

/**
 * The type Reviews.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Reviews {
    @XmlElement(name = "review")
    private List<Review> reviewList = new ArrayList<>();

    /**
     * Gets review list.
     *
     * @return the review list
     */
// Getters and setters
    public List<Review> getReviewList() {
        return reviewList;
    }

    /**
     * Sets review list.
     *
     * @param reviewList the review list
     */
    public void setReviewList(List<Review> reviewList) {
        this.reviewList = reviewList;
    }
}
