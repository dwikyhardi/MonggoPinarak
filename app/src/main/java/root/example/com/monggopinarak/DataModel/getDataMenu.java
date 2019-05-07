package root.example.com.monggopinarak.DataModel;

public class getDataMenu {
    private String Name;
    private String Price;
    private String Status;
    private String MenuId;

    public getDataMenu(String Name,
                       String Price,
                       String Status,
                       String MenuId) {
        this.setName(Name);
        this.setPrice(Price);
        this.setStatus(Status);
        this.setMenuId(MenuId);

    }


    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPrice() {
        return Price;
    }

    public void setPrice(String price) {
        Price = price;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getMenuId() {
        return MenuId;
    }

    public void setMenuId(String menuId) {
        MenuId = menuId;
    }
}
