package com.tutor.noviolence

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class CoroutineAsyncTask<Params, Progress, Result> {

    //open fun onPreExecute(){}

    abstract fun doInBackground(vararg params: Params?): Result

    // open fun onProgressUpdate(vararg values: Progress?){}

    open fun onPostExecute(result: Result?){}

    /*open fun onCancelled(result: Result?){}
    protected fun publishProgress(vararg progress: Progress?){
        GlobalScope.launch(Dispatchers.Main){
            onProgressUpdate(*progress)
        }
    }*/

    fun execute(vararg params: Params?){

        GlobalScope.launch(Dispatchers.Default){
            val result = doInBackground(*params)

            withContext(Dispatchers.Main){
                onPostExecute(result)
            }
        }

    }

    /*fun cancel(mayInterruptedIfRunning: Boolean){
    }*/


}