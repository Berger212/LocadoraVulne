const state = {
    token: '',
};

const consoleBox = document.getElementById('consoleSaida');
const tokenLabel = document.getElementById('currentToken');
const bypassToggle = document.getElementById('bypassToggle');
const bypassValue = document.getElementById('bypassValue');

const renderJson = (targetId, data) => {
    const el = document.getElementById(targetId);
    el.textContent = JSON.stringify(data, null, 2);
};

const logResponse = (title, payload, isError = false) => {
    const prefix = `[${new Date().toLocaleTimeString()}] ${title}`;
    const content = typeof payload === 'string' ? payload : JSON.stringify(payload, null, 2);
    consoleBox.textContent = `${prefix}\n${content}\n\n${consoleBox.textContent}`;
    if (isError) {
        console.warn(title, payload);
    } else {
        console.info(title, payload);
    }
};

const setToken = (token) => {
    state.token = token;
    tokenLabel.textContent = token || '—';
};

document.getElementById('btnLimparToken').addEventListener('click', () => setToken(''));

const buildHeaders = (hasBody = true) => {
    const headers = {
        'Accept': 'application/json'
    };
    if (hasBody) {
        headers['Content-Type'] = 'application/json';
    }
    if (state.token) {
        headers['Authorization'] = `Bearer ${state.token}`;
    }
    if (bypassToggle.checked && bypassValue.value) {
        headers['X-Bypass-Auth'] = bypassValue.value;
    }
    return headers;
};

const apiFetch = async (path, options = {}) => {
    const config = {
        method: options.method || 'GET',
        headers: buildHeaders(Boolean(options.body)),
    };
    if (options.body) {
        config.body = JSON.stringify(options.body);
    }
    const response = await fetch(path, config);
    const text = await response.text();
    let payload;
    try {
        payload = text ? JSON.parse(text) : null;
    } catch (err) {
        payload = text;
    }
    if (!response.ok) {
        throw { status: response.status, payload };
    }
    return payload;
};

const formToJson = (form) => Object.fromEntries(new FormData(form).entries());

// Usuários

document.getElementById('formCreateUser').addEventListener('submit', async (event) => {
    event.preventDefault();
    const body = formToJson(event.target);
    try {
        const result = await apiFetch('/usuarios', { method: 'POST', body });
        logResponse('Usuário criado', result);
    } catch (error) {
        logResponse('Erro ao criar usuário', error, true);
    }
});

document.getElementById('formLogin').addEventListener('submit', async (event) => {
    event.preventDefault();
    const body = formToJson(event.target);
    try {
        const result = await apiFetch('/login', { method: 'POST', body });
        setToken(result.token);
        logResponse('Login efetuado', result);
    } catch (error) {
        logResponse('Erro ao autenticar', error, true);
    }
});

// Jogos

document.getElementById('formCreateJogo').addEventListener('submit', async (event) => {
    event.preventDefault();
    const body = formToJson(event.target);
    body.precoDiaria = Number(body.precoDiaria);
    try {
        const result = await apiFetch('/jogos', { method: 'POST', body });
        logResponse('Jogo criado', result);
        await listarJogos();
    } catch (error) {
        logResponse('Erro ao criar jogo', error, true);
    }
});

document.getElementById('formUpdateJogo').addEventListener('submit', async (event) => {
    event.preventDefault();
    const data = formToJson(event.target);
    const id = data.id;
    const body = {
        titulo: data.titulo,
        genero: data.genero,
        precoDiaria: Number(data.precoDiaria),
        descricao: data.descricao,
    };
    try {
        const result = await apiFetch(`/jogos/${id}`, { method: 'PUT', body });
        logResponse('Jogo atualizado', result);
        await listarJogos();
    } catch (error) {
        logResponse('Erro ao atualizar jogo', error, true);
    }
});

document.getElementById('formDeleteJogo').addEventListener('submit', async (event) => {
    event.preventDefault();
    const { id } = formToJson(event.target);
    try {
        await apiFetch(`/jogos/${id}`, { method: 'DELETE' });
        logResponse('Jogo removido', { id });
        await listarJogos();
    } catch (error) {
        logResponse('Erro ao remover jogo', error, true);
    }
});

const listarJogos = async () => {
    try {
        const result = await apiFetch('/jogos');
        renderJson('jogosSaida', result);
        logResponse('Listagem de jogos', result);
    } catch (error) {
        logResponse('Erro ao listar jogos', error, true);
    }
};

document.getElementById('btnListarJogos').addEventListener('click', listarJogos);

// Clientes

document.getElementById('formCreateCliente').addEventListener('submit', async (event) => {
    event.preventDefault();
    const body = formToJson(event.target);
    try {
        const result = await apiFetch('/clientes', { method: 'POST', body });
        logResponse('Cliente criado', result);
        await listarClientes();
    } catch (error) {
        logResponse('Erro ao criar cliente', error, true);
    }
});

const listarClientes = async () => {
    try {
        const result = await apiFetch('/clientes');
        renderJson('clientesSaida', result);
        logResponse('Listagem de clientes', result);
    } catch (error) {
        logResponse('Erro ao listar clientes', error, true);
    }
};

document.getElementById('btnListarClientes').addEventListener('click', listarClientes);

// Locações

document.getElementById('formCreateLocacao').addEventListener('submit', async (event) => {
    event.preventDefault();
    const body = formToJson(event.target);
    body.clienteId = Number(body.clienteId);
    body.jogoId = Number(body.jogoId);
    body.dias = Number(body.dias);
    try {
        const result = await apiFetch('/locacoes', { method: 'POST', body });
        logResponse('Locação criada', result);
        await listarLocacoes();
    } catch (error) {
        logResponse('Erro ao criar locação', error, true);
    }
});

document.getElementById('formDevolucao').addEventListener('submit', async (event) => {
    event.preventDefault();
    const { id, data } = formToJson(event.target);
    try {
        const result = await apiFetch(`/locacoes/${id}/devolucao`, {
            method: 'PUT',
            body: { dataDevolucao: data }
        });
        logResponse('Devolução registrada', result);
        await listarLocacoes();
    } catch (error) {
        logResponse('Erro na devolução', error, true);
    }
});

const listarLocacoes = async () => {
    try {
        const result = await apiFetch('/locacoes');
        renderJson('locacoesSaida', result);
        logResponse('Listagem de locações', result);
    } catch (error) {
        logResponse('Erro ao listar locações', error, true);
    }
};

document.getElementById('btnListarLocacoes').addEventListener('click', listarLocacoes);

// Carrega listagens iniciais ao abrir a página
listarJogos();
listarClientes();
listarLocacoes();
