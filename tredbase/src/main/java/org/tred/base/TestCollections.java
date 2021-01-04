//
//// Actor model
//// 1. Threading. locks.
//// 2. Actor.  Messages different process.
//
///**
// ActorSystem actorSystem;
//
//
// actorSystem.start();
// actorSystem.add(actor);
//
//
// // Asynchornous return immediately
// actorSystem.tell(actor, message);
//
// //
// result = actorSystem.ask(actor, message);
//
// class Actor {
//
//
// public Object receive(Message message, State map) {
// // process
// // return result
// }
// }
// */
//
//class Actor implements Runnable{
//
//    class Payload{
//        String message;
//        String responseId;
//    }
//    Lock lock = ;
//    Condition done = lock.newCondition;
//    Map<String, Object> responses ;
//
//    static class FutureRespnse{
//
//    }
//    volatile boolean shutdown =false;
//
//
//    public  String offerAsk(String message){
//        String responseId = new UUID().uuid().toString();
//        jobQueue.offer(new Payload(message, responseId));
//        responses.put(responseId, null);
//        return responseId;
//
//
//    }
//    public  void offerTell(String message){
//        jobQueue.offer(new Payload(message, ""));
//    }
//
//
//    public Object get(String responseId){
//        while(responses.get(responsesId) == null){
//            awai
//        }
//        responses.get(responseId);
//    }
//
//    BlockingQueue<Payload> jobQueue = new ArrayBlockingQueue<Payload>();
//
//
//    String id;
//    public Object receive(Message message) {
//        // process
//        // return result
//    }
//
//    public Object receive(Message message, State map) {
//        // process
//        // return result
//    }
//
//
//    public void run(){
//        while(!shutdown){
//            Payload p = jobQueue.take();
//            if(!p.responseId.isEmpty()){
//                responses.put(p.responseId, receive(p.message));
//            }
//
//        }
//    }
//}
//
//class ActorSystem{
//
//    Map<String, Actor> actorMap = new HashMap<>();
//
//    ExecutorService es;
//
//    ActorSystem() {
//        es = Executors.newFixedThreadPool(10);
//    }
//    public void add(Actor action){
//        actorMap.put(action.id, action);
//        es.execute(action);
//
//    }
//
//    public void start(){
//
//        // actorMap.forEach(a -> {
//        //     es.execute(a);
//        // });
//
//    }
//
//    public synchronized void tell(String actorId, String message){
//        Actor actor = actorMap.get(actorId);
//        actor.offer(message);
//    }
//
//
//    public synchronized Object ask(String actorId, String message){
//        Actor actor = actorMap.get(actorId);
//        String responseId  = actor.offerAsk(message);
//        return actor.get(responseId);
//    }
//
//}