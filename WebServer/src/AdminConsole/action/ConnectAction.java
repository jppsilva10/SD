package AdminConsole.action;

import AdminConsole.model.UserBean;
import com.company.NotFoundException;
import com.github.scribejava.core.model.OAuth2AccessToken;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.SessionAware;

import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ConnectAction extends ActionSupport implements SessionAware {
    private static final long serialVersionUID = 4L;
    private Map<String, Object> session;
    private String codigo;
    private String code;
    private String state;

    public void setCodigo(String codigo){
        this.codigo = codigo;
    }
    public void setCode(String code){
        this.code = code;
    }
    public void setState(String state){
        this.state = state;
    }

    public String execute() {
        UserBean ub;
        try {
            ub = getUserBean();
        } catch (NotBoundException e) {
            return "fail";
        } catch (RemoteException e) {
            return "fail";
        }

        if(ub.getLogin()){

            try{
                ub.setAccessToken(code);
            } catch (NotFoundException.PessoaNF pessoaNF) {
                return "login";
            } catch (RemoteException e) {
                return "fail";
            } catch (IOException e) {
                return "erro";
            } catch (InterruptedException e) {
                return "erro";
            } catch (ExecutionException e) {
                return "erro";
            }catch (Exception e){
                return "erro";
            }
            return "success";
        }
        else{
            return "login";
        }
    }

    public UserBean getUserBean() throws NotBoundException, RemoteException {
        if(!session.containsKey("UserBean"))
            this.setUserBean(new UserBean());
        return (UserBean) session.get("UserBean");
    }

    public void setUserBean(UserBean ub) {
        this.session.put("UserBean", ub);
    }

    @Override
    public void setSession(Map<String, Object> session) {
        this.session = session;
    }
}
