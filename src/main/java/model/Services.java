package model;

public class Services {

    private Long id;
    private String name;
    private String  headcount;
    private String description;

    /*------------SINGLETON--------------*/
    private static Services instance;

    public static Services getInstance(){
        if(instance==null){
            instance = new Services();
        }
        return instance;
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHeadcount() {
        return headcount;
    }

    public void setHeadcount(String headcount) {
        this.headcount = headcount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
