function f = loadres(filename)

res.esnodb      = -200:200;
res.nsim        = zeros(1,numel(res.esnodb));
res.nerractual  = zeros(1,numel(res.esnodb));
res.nerrdet     = zeros(1,numel(res.esnodb));
res.nerrmiss    = zeros(1,numel(res.esnodb));
res.nerrfalse   = zeros(1,numel(res.esnodb));

if exist(filename, 'file') == 2
    load(filename,'esnodb','nsim','nerractual','nerrdet','nerrmiss','nerrfalse');
    res.esnodb     = res.esnodb;
    res.nsim       = nsim;
    res.nerractual = nerractual;
    res.nerrdet     = nerrdet;
    res.nerrmiss = nerrmiss;
    res.nerrfalse  = nerrfalse;
else
    nsim       = res.esnodb;
    esnodb     = res.esnodb;
    nerractual = res.nerractual;
    nerrdet    = res.nerrdet;
    nerrmiss   = res.nerrmiss;
    nerrfalse  = res.nerrfalse;
    save(filename,'esnodb','nsim','nerractual','nerrdet','nerrmiss','nerrfalse');
end

f = res;

end