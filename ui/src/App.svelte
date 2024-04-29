<script lang="ts">
  import {Bridge, init} from './lib/bridge';  
  import { Button, GradientButton, Progressbar, Spinner } from 'flowbite-svelte';
  import MenuBar from './lib/MenuBar.svelte';
    import { onMount } from 'svelte';
    import ProfileSelector from './lib/ProfileSelector.svelte';
    import PackSelector from './lib/PackSelector.svelte';
    const bgCount=4;
    var bgCurrent=parseInt((Math.random()*4).toFixed())
    if(bgCurrent==bgCount)
      bgCurrent=0;
   /* setInterval(()=>{
      if(bgCurrent+1>=bgCount)
        bgCurrent=0;
      else
        bgCurrent++
    },10000)*/
    var isGameReady=false;
    var progress=100;
    var status="Prêt au lancement!";
    var ready=false;
  onMount(()=>{
    setTimeout(() => {
      init(); 
      ready=true;
      Bridge.on("packsReady",(r)=>isGameReady=r!="true")
      checkLocked();
      Bridge.on("progress",(r)=>progress=parseInt(r))
      Bridge.on("status",(r)=>status=r)
    }, 500);
  })
  function checkLocked()
  {
    Bridge.isPackLocked().then(l=>isGameReady=!l)
  }
</script>
<main class="flex h-screen bg-gray-800 select-none relative" style='background-image: url("{"/bg/"+bgCurrent+".png"}");'>
  
  <h1 class="text-white text-3xl font-bold mx-auto top-0 left-0 right-0 absolute text-center mt-5">Bienvenue</h1>

  {#if ready}
  <div class="w-3/12"><MenuBar/></div>
  <div class="w-5/12 flex flex-wrap items-end justify-center mt-24">
      <div class="fixed bottom-0.5 left-0.5 w-full progress-bar-background">
        <div class="w-full flex justify-center s-XsEmFtvddWTw"> 
          <div class="w-full flex flex-col ">
            <div class="mb-1 font-medium dark:text-white text-left ml-1">{status}</div>
            <Progressbar progress={progress} size="h-4" labelInside class="w-full" />
           
        </div> 
        <div class="flex justify-between progress-button-container w-2/5">
          <GradientButton disabled={!isGameReady} color="green" size="xl" shadow pill class="w-25pct ml-2 w-full" on:click={Bridge.startGame}>Jouer</GradientButton>
      </div>       
      </div>
      </div>
  </div>

  <div class="fixed top-60 right-1 mr-10 mb-5">
    <ProfileSelector on:refresh={checkLocked}/><PackSelector/>
  </div>
  {:else}
  <div class="flex items-center flex-col w-full justify-center">
      <Spinner color=green size={20} class="text-center "/>
  </div>
  {/if}
</main>


<style>
  main{
   background-size: cover;
   /* -webkit-transition: background-image 2s ease-in-out;
    transition: background-image 2s ease-in-out; Pas de transition pour le moment avec javafx, elles devraient être dispo autour de la version 23+12*/
  }

  .progress-bar-background {
  background-color: rgba(51, 51, 51, 0.75); 
  border-radius: 16px 16px 0 0; 
  padding: 16px; 
}
</style>
