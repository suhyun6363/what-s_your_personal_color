package kr.ac.duksung.mycol;

public class Product {
    private String name;
    private String optionName;
    private String imageUrl;
    private String number;
    private String optionRgb;

    public Product(String name, String optionName, String imageUrl, String number, String optionRgb) {
        this.name = name;
        this.optionName = optionName;
        this.imageUrl = imageUrl;
        this.number = number;
        this.optionRgb = optionRgb; // 필드 초기화
    }

    // 이미지 URL을 받는 생성자 추가
    public Product(String imageUrl) {
        this.imageUrl = imageUrl;
    }


    public String getName() {
        if (name != null) {
            int firstSpaceIndex = name.indexOf(" ");
            if (firstSpaceIndex != -1) {
                String firstPart = name.substring(0, firstSpaceIndex);
                String secondPart = name.substring(firstSpaceIndex + 1);
                return firstPart + "\n" + secondPart;
            } else {
                return name;
            }
        } else {
            return "";
        }
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getNumber() {
        return number;
    }

    public String getOptionRgb() {
        return optionRgb;
    }

    public void setOptionRgb(String optionRgb) {
        this.optionRgb = optionRgb;
    }
}
