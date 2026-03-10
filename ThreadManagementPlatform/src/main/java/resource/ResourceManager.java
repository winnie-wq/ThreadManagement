package resource;

import java.util.concurrent.Semaphore;

public class ResourceManager {

    private Semaphore cpuResource;

    public ResourceManager(int cpuCount){

        cpuResource=new Semaphore(cpuCount);

    }

    public void acquire(){

        try{

            cpuResource.acquire();

        }catch(Exception e){

            e.printStackTrace();

        }

    }

    public void release(){

        cpuResource.release();

    }

}