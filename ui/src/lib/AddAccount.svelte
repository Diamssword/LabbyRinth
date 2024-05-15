<script>
    import { Button, Modal, Label, Input, Checkbox, Spinner, Alert } from 'flowbite-svelte';
    import { Bridge } from './Bridge';
    import { createEventDispatcher } from 'svelte';
    export let formModal = false;
    var isWaiting=false;
    var email="";
    var alert=false;
    const disp=createEventDispatcher();
    function auth()
    {
        if(email && email.length>1)
        {
            alert=false;
            isWaiting=true;
            Bridge.addAccount(email).then(e=>{
                isWaiting=false;
                if(e==true)
                {
                    disp("refresh")
                    alert=false;
                    formModal=false;
                }
                else
                {
                    alert=true;
                }
            })
        }
    }
  </script>
  
  <Modal bind:open={formModal} size="xs" autoclose={false} class="w-full">
    <div class="flex flex-col space-y-6">
      <h3 class="mb-4 text-xl font-medium text-gray-900 dark:text-white">Authentifiez vous</h3>
        {#if isWaiting}
         
            <span class=" dark:text-primary-400">Une fenêtre s'est ouverte dans votre navigateur pour finir votre identification.</span>
         
            <div class="flex justify-center space-y-1 text-sm dark:text-primary-400">
                <Spinner size=12/>
            </div>
            
            <span  class=" dark:text-primary-400">Une fois identifié, vous pouvez fermer la page et revenir ici!</span>
        {:else}
        {#if alert}
            <Alert color="yellow" border>
                <span class="font-medium">Erreur!</span>
                Authentification échouée, veuillez réessayer.
            </Alert>
        {/if}
            <Label class="space-y-2">
                <span>Email</span>
                <Input type="email" name="email" placeholder="Email de votre compte" bind:value={email} required />
            </Label>
            <Button type="button" class="w-full1" on:click={auth}>Connexion</Button>
            <div class="flex flex-col space-y-1 text-sm dark:text-primary-400">
            <span >Saissisez l'email de votre compte minecraft.</span>
            <span>Une fenêtre s'ouvrira dans votre navigateur pour finir votre identification.</span>
            </div>
        {/if}
    </div>
  </Modal>