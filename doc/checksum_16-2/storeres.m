function f = storeres(filename, results, esnodb, nsim, nerractual, nerrdet, nerrmiss, nerrfalse)

results.esnodb       (results.esnodb == esnodb) = esnodb;
results.nsim         (results.esnodb == esnodb) = results.nsim       (results.esnodb == esnodb) + nsim;
results.nerractual   (results.esnodb == esnodb) = results.nerractual (results.esnodb == esnodb) + nerractual;
results.nerrdet      (results.esnodb == esnodb) = results.nerrdet    (results.esnodb == esnodb) + nerrdet;
results.nerrmiss     (results.esnodb == esnodb) = results.nerrmiss   (results.esnodb == esnodb) + nerrmiss;
results.nerrfalse    (results.esnodb == esnodb) = results.nerrfalse  (results.esnodb == esnodb) + nerrfalse;

nsim       = results.nsim;
esnodb     = results.esnodb;
nerractual = results.nerractual;
nerrdet    = results.nerrdet;
nerrmiss   = results.nerrmiss;
nerrfalse  = results.nerrfalse;

save(filename,'esnodb','nsim','nerractual','nerrdet','nerrmiss','nerrfalse');

end